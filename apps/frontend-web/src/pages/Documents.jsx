import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Documents() {
  const { API_URL, GATEWAY_PORT, getHeaders, logout, estudianteId } = useOutletContext();

  const [resumen, setResumen] = useState({
    totalDocumentos: 0,
    documentos: []
  });
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState(null);

  const cargarDocumentos = () => {
    if (!estudianteId) return;
    fetch(`${API_URL}:${GATEWAY_PORT}/api/documents/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) {
          // Tratar como "sin documentos todavía"
          return {
            totalDocumentos: 0,
            documentos: []
          };
        }
        if (!response.ok) throw new Error("Error al cargar documentos");
        return response.json();
      })
      .then(data => {
        setResumen(data);
        setLoading(false);
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
        setLoading(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      cargarDocumentos();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  const getDocumentIcon = (tipo) => {
    if (tipo === 'CARTA_FINALIZACION') return '📜';
    return '📄';
  };

  const getDocumentTypeName = (tipo) => {
    if (tipo === 'CERTIFICADO_HORAS') return 'Certificado de Horas';
    if (tipo === 'CARTA_FINALIZACION') return 'Carta de Finalización';
    return tipo || 'Documento';
  };

  const formatFecha = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      // Split to display only YYYY-MM-DD if ISO format
      return dateStr.substring(0, 10);
    } catch {
      return dateStr;
    }
  };

  if (!estudianteId) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-gray-500 font-medium text-lg animate-pulse">Cargando perfil...</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-1 space-y-6">
      {/* Header and Refresh */}
      <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
        <h2 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="mr-2">📂</span> Mis Documentos
        </h2>
        <button 
          type="button"
          className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
          onClick={() => { setLoading(true); cargarDocumentos(); }}
        >
          🔄 Actualizar
        </button>
      </div>

      {mensaje && (
        <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200">
          {mensaje.text}
        </div>
      )}

      {/* Header Contador */}
      {!loading && resumen.totalDocumentos > 0 && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-2 text-left">
          <p className="text-gray-600 text-lg">
            Tienes <span className="text-2xl font-bold text-[var(--color-purple)]">{resumen.totalDocumentos}</span> documentos generados
          </p>
        </div>
      )}

      {/* Documents List */}
      <div className="space-y-4">
        {loading ? (
          <div className="text-gray-400 text-sm text-center py-10">
            Cargando documentos...
          </div>
        ) : resumen.documentos.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center text-gray-400 text-sm leading-relaxed">
            Aún no se han generado documentos. Se generan automáticamente cuando se validan tus horas.
          </div>
        ) : (
          resumen.documentos.map((doc, idx) => (
            <div key={doc.documentoId || idx} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex items-center justify-between gap-4">
              <div className="flex items-center space-x-4 min-w-0 text-left">
                <div className="text-3xl shrink-0 p-3 bg-gray-50 rounded-xl border border-gray-100">
                  {getDocumentIcon(doc.tipo)}
                </div>
                <div className="min-w-0">
                  <h3 className="text-base font-bold text-gray-900 truncate">
                    {getDocumentTypeName(doc.tipo)}
                  </h3>
                  <p className="text-xs text-gray-500 mt-1">
                    📅 Creado: {formatFecha(doc.createdAt)}
                  </p>
                </div>
              </div>

              {/* Download link formatted as a button */}
              <a
                href={doc.s3Url}
                target="_blank"
                rel="noopener noreferrer"
                className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white text-sm font-semibold rounded-lg py-2.5 px-4 transition shadow-sm whitespace-nowrap inline-block text-center"
              >
                ⬇️ Descargar
              </a>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Documents;
