/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Reports() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}:8082`;

  const token = localStorage.getItem('token');
  let payload = {};
  if (token && token.split('.').length === 3) {
    try { payload = JSON.parse(atob(token.split('.')[1])); }
    catch (e) { console.error('JWT decode error:', e); }
  }
  const estudianteId = payload.id || payload.userId || payload.sub;
  const userRol = (payload.rol || payload.role || '').toUpperCase();
  const isAcademicStaff = userRol.includes('TUTOR') || userRol.includes('COORDINADOR') || userRol.includes('ADMIN');

  // --- States ---
  const [studentReport, setStudentReport] = useState(null);
  const [globalReport, setGlobalReport] = useState(null);
  
  const [loadingStudent, setLoadingStudent] = useState(true);
  const [loadingGlobal, setLoadingGlobal] = useState(false);
  
  const [errorStudent, setErrorStudent] = useState(null);
  const [errorGlobal, setErrorGlobal] = useState(null);
  
  const [reportType, setReportType] = useState("horas");
  const [bannerMessage, setBannerMessage] = useState(null);

  // --- API Fetch ---
  const cargarReporteEstudiante = () => {
    if (!estudianteId) return;
    setErrorStudent(null);
    fetch(`${API}/api/reports/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return null;
        if (!response.ok) throw new Error("Error al cargar reporte de estudiante");
        return response.json();
      })
      .then(data => {
        setStudentReport(data);
        setLoadingStudent(false);
      })
      .catch(error => {
        setErrorStudent(error.message);
        setLoadingStudent(false);
      });
  };

  const cargarReporteGlobal = () => {
    if (!isAcademicStaff) return;
    setLoadingGlobal(true);
    setErrorGlobal(null);
    fetch(`${API}/api/reports/global`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return null;
        if (!response.ok) throw new Error("Error al cargar reporte global");
        return response.json();
      })
      .then(data => {
        setGlobalReport(data);
        setLoadingGlobal(false);
      })
      .catch(error => {
        setErrorGlobal(error.message);
        setLoadingGlobal(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      cargarReporteEstudiante();
    }
    if (isAcademicStaff) {
      cargarReporteGlobal();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  const handleGenerateReport = (e) => {
    e.preventDefault();
    setBannerMessage({ text: "Generación de reportes en desarrollo", type: "info" });
    setTimeout(() => setBannerMessage(null), 4000);
  };

  const formatFecha = (dateStr) => {
    if (!dateStr) return '—';
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('es-EC', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-1 space-y-6 text-left">
      {/* Title Header */}
      <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
        <h2 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="mr-2">📊</span> Reportes del Sistema
        </h2>
        <button 
          type="button"
          className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
          onClick={() => {
            setLoadingStudent(true);
            cargarReporteEstudiante();
            if (isAcademicStaff) cargarReporteGlobal();
          }}
        >
          🔄 Actualizar
        </button>
      </div>

      {bannerMessage && (
        <div className="bg-blue-50 text-blue-700 text-sm p-4 rounded-xl font-medium border border-blue-200 shadow-sm animate-fade-in">
          ℹ️ {bannerMessage.text}
        </div>
      )}

      {/* Generator Section */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <h3 className="text-base font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center">
          <span className="mr-2">⚡</span> Generar Nuevo Reporte Consolidado
        </h3>
        
        <form onSubmit={handleGenerateReport} className="flex flex-col sm:flex-row items-end gap-4">
          <div className="flex flex-col flex-1 w-full text-left">
            <label className="text-xs font-semibold text-gray-500 mb-1.5 uppercase tracking-wide">Tipo de Reporte:</label>
            <select
              value={reportType}
              onChange={(e) => setReportType(e.target.value)}
              className="bg-gray-100 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] appearance-none cursor-pointer w-full border border-transparent"
            >
              <option value="horas">Consolidado de Horas</option>
              <option value="evaluaciones">Resumen de Evaluaciones</option>
              <option value="pasantías">Ofertas de Pasantías</option>
              <option value="vinculación">Proyectos de Vinculación</option>
            </select>
          </div>
          <button
            type="submit"
            className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white text-sm font-semibold rounded-lg py-2.5 px-6 transition shadow-sm w-full sm:w-auto shrink-0 h-[42px]"
          >
            Generar Reporte
          </button>
        </form>
      </div>

      {/* Student Specific Reports Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <h3 className="text-base font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4">
          📋 Historial de Reportes del Estudiante
        </h3>

        {loadingStudent ? (
          <div className="text-gray-400 text-sm text-center py-10 animate-pulse">
            Cargando reportes...
          </div>
        ) : errorStudent ? (
          <div className="text-sm text-red-600 bg-red-50 p-4 rounded-xl border border-red-100 text-center">
            ⚠️ Error al cargar: {errorStudent}
          </div>
        ) : !studentReport ? (
          <div className="text-gray-400 text-sm text-center py-10">
            Aún no se ha generado ningún reporte. Registra actividades de horas para crearlo de forma automática.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-600 border-collapse">
              <thead>
                <tr className="border-b border-gray-100 text-xs font-semibold text-gray-400 uppercase">
                  <th className="py-3 px-2">ID</th>
                  <th className="py-3 px-2">Tipo</th>
                  <th className="py-3 px-2">Fecha Generación</th>
                  <th className="py-3 px-2">Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                  <td className="py-4 px-2 font-mono font-semibold text-gray-900">REP-00{studentReport.id}</td>
                  <td className="py-4 px-2 font-medium text-gray-800">
                    Consolidado Académico ({studentReport.totalHorasValidadas}h aprobadas, {studentReport.totalDocumentos} documentos)
                  </td>
                  <td className="py-4 px-2 text-gray-500">{formatFecha(studentReport.ultimaActualizacion)}</td>
                  <td className="py-4 px-2">
                    <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-700 border border-green-200">
                      COMPLETADO
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Global Reports Section (Visible only to Coordinator/Tutor) */}
      {isAcademicStaff && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h3 className="text-base font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center justify-between">
            <span>🌐 Reporte Global Consolidado (MongoDB)</span>
            <span className="text-xs font-bold text-[var(--color-purple)] bg-purple-50 px-2 py-0.5 rounded border border-purple-200">VISTA PERSONAL DOCENTE</span>
          </h3>

          {loadingGlobal ? (
            <div className="text-gray-400 text-sm text-center py-10 animate-pulse">
              Cargando reporte global...
            </div>
          ) : errorGlobal ? (
            <div className="text-sm text-red-600 bg-red-50 p-4 rounded-xl border border-red-100 text-center">
              ⚠️ Error al cargar: {errorGlobal}
            </div>
          ) : !globalReport ? (
            <div className="text-gray-400 text-sm text-center py-10">
              No hay datos de reportes globales disponibles.
            </div>
          ) : (
            <div className="space-y-4">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-gray-600 border-collapse">
                  <thead>
                    <tr className="border-b border-gray-100 text-xs font-semibold text-gray-400 uppercase">
                      <th className="py-3 px-2">ID</th>
                      <th className="py-3 px-2">Tipo</th>
                      <th className="py-3 px-2">Fecha Generación</th>
                      <th className="py-3 px-2">Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                      <td className="py-4 px-2 font-mono font-semibold text-gray-900">REP-{globalReport.id ? globalReport.id.toUpperCase() : 'N/A'}</td>
                      <td className="py-4 px-2 font-medium text-gray-800">
                        Consolidado General ({globalReport.totalEstudiantes || 0} estudiantes, {globalReport.totalHorasValidadas || 0}h validadas)
                      </td>
                      <td className="py-4 px-2 text-gray-500">{formatFecha(globalReport.ultimaActualizacion)}</td>
                      <td className="py-4 px-2">
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-700 border border-green-200">
                          COMPLETADO
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              {/* Aggregated Faculties info */}
              {globalReport.estudiantesPorFacultad && Object.keys(globalReport.estudiantesPorFacultad).length > 0 && (
                <div className="bg-gray-50 border border-gray-100 rounded-xl p-4 mt-2">
                  <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wide mb-3">Distribución por Facultad / Carrera:</h4>
                  <div className="flex flex-wrap gap-2">
                    {Object.entries(globalReport.estudiantesPorFacultad).map(([key, val]) => (
                      <span key={key} className="bg-white px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-700 shadow-sm">
                        🎓 {key}: <span className="text-[var(--color-purple)] font-black">{val}</span>
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Reports;
