/* eslint-disable react-hooks/set-state-in-effect */
/* eslint-disable react-hooks/exhaustive-deps */
import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';

function Encuestas() {
  useOutletContext();
  
  // JWT Parsing
  const token = localStorage.getItem('token');
  let jwtPayload = {};
  if (token && token.split('.').length === 3) {
    try {
      jwtPayload = JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error('Error al decodificar JWT:', e);
    }
  }

  const estudianteId = jwtPayload.id || jwtPayload.userId || jwtPayload.sub || 'estudiante_id_placeholder';
  const nombreEstudiante = jwtPayload.nombre || jwtPayload.name || 'Estudiante UCE';

  // State Management
  const [encuestas, setEncuestas] = useState([]);
  const [empresa, setEmpresa] = useState('');
  const [calificacion, setCalificacion] = useState(5);
  const [comentario, setComentario] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingList, setLoadingList] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Fetch surveys from Supabase
  const fetchEncuestas = async () => {
    if (!supabase) {
      setError('El servicio de encuestas (Supabase) no está configurado en este entorno.');
      setLoadingList(false);
      return;
    }
    setLoadingList(true);
    try {
      const { data, error: fetchErr } = await supabase
        .from('encuestas_satisfaccion')
        .select('*')
        .eq('estudiante_id', estudianteId)
        .order('id', { ascending: false });

      if (fetchErr) throw fetchErr;
      setEncuestas(data || []);
    } catch (err) {
      console.error('Error al cargar encuestas:', err.message);
      setError('No se pudieron cargar las encuestas anteriores.');
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    if (!supabase) {
      setError('El servicio de encuestas (Supabase) no está configurado en este entorno.');
      setLoadingList(false);
      return;
    }
    if (estudianteId) {
      fetchEncuestas();
    }
  }, [estudianteId]);

  // Submit Survey
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!supabase) {
      setError('El servicio de encuestas (Supabase) no está configurado en este entorno.');
      return;
    }
    setLoading(true);
    setError(null);
    setSuccessMsg(null);

    if (!empresa.trim()) {
      setError('El nombre de la empresa es obligatorio.');
      setLoading(false);
      return;
    }

    try {
      const { error: insertErr } = await supabase
        .from('encuestas_satisfaccion')
        .insert([
          {
            estudiante_id: estudianteId,
            nombre_estudiante: nombreEstudiante,
            empresa: empresa.trim(),
            calificacion: parseInt(calificacion),
            comentario: comentario.trim()
          }
        ]);

      if (insertErr) throw insertErr;

      setSuccessMsg('¡Encuesta enviada con éxito!');
      setEmpresa('');
      setCalificacion(5);
      setComentario('');
      // Refresh list
      fetchEncuestas();
    } catch (err) {
      console.error('Error al insertar encuesta:', err.message);
      setError('Hubo un error al enviar la encuesta. Por favor, intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 text-white">
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-white mb-2">
          Encuestas de Satisfacción Post-Práctica 📋
        </h1>
        <p className="text-gray-400">
          Tu opinión nos ayuda a mejorar los proyectos de vinculación y pasantías.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Formulario */}
        <div className="lg:col-span-1 bg-white/5 backdrop-blur-md rounded-2xl border border-white/10 p-6 shadow-xl">
          <h2 className="text-xl font-semibold mb-6 border-b border-white/10 pb-3 text-[var(--color-gold)]">
            Nueva Encuesta
          </h2>

          {error && (
            <div className="mb-4 p-4 rounded-xl bg-red-500/20 border border-red-500/30 text-red-200 text-sm">
              {error}
            </div>
          )}

          {successMsg && (
            <div className="mb-4 p-4 rounded-xl bg-emerald-500/20 border border-emerald-500/30 text-emerald-200 text-sm">
              {successMsg}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label htmlFor="empresa" className="block text-sm font-medium text-gray-300 mb-2">
                Empresa / Institución
              </label>
              <input
                id="empresa"
                type="text"
                value={empresa}
                onChange={(e) => setEmpresa(e.target.value)}
                placeholder="Ej. UCE, Telconet, etc."
                className="w-full px-4 py-2.5 bg-black/30 border border-white/10 rounded-xl text-white focus:outline-none focus:border-[var(--color-purple)] focus:ring-1 focus:ring-[var(--color-purple)]"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Calificación
              </label>
              <div className="flex items-center space-x-2">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setCalificacion(star)}
                    className="text-2xl focus:outline-none transition-transform hover:scale-110"
                  >
                    <span className={star <= calificacion ? "text-amber-400" : "text-gray-600"}>
                      ★
                    </span>
                  </button>
                ))}
                <span className="text-sm text-gray-400 ml-2">({calificacion} de 5)</span>
              </div>
            </div>

            <div>
              <label htmlFor="comentario" className="block text-sm font-medium text-gray-300 mb-2">
                Comentarios o Sugerencias
              </label>
              <textarea
                id="comentario"
                value={comentario}
                onChange={(e) => setComentario(e.target.value)}
                placeholder="Cuéntanos tu experiencia y sugerencias..."
                rows="4"
                className="w-full px-4 py-2.5 bg-black/30 border border-white/10 rounded-xl text-white focus:outline-none focus:border-[var(--color-purple)] focus:ring-1 focus:ring-[var(--color-purple)] resize-none"
              ></textarea>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-[var(--color-purple)] hover:bg-[var(--color-purple)]/90 active:scale-95 disabled:opacity-50 text-white font-semibold rounded-xl transition-all shadow-lg shadow-[var(--color-purple)]/30"
            >
              {loading ? 'Enviando...' : 'Enviar Encuesta'}
            </button>
          </form>
        </div>

        {/* Historial */}
        <div className="lg:col-span-2 bg-white/5 backdrop-blur-md rounded-2xl border border-white/10 p-6 shadow-xl flex flex-col h-[500px]">
          <h2 className="text-xl font-semibold mb-6 border-b border-white/10 pb-3 text-[var(--color-gold)]">
            Mis Encuestas Enviadas
          </h2>

          <div className="flex-1 overflow-y-auto space-y-4 pr-2 scrollbar-thin scrollbar-thumb-white/10">
            {loadingList ? (
              <div className="flex justify-center items-center h-full">
                <span className="text-gray-400 text-sm">Cargando historial...</span>
              </div>
            ) : encuestas.length === 0 ? (
              <div className="flex flex-col justify-center items-center h-full text-gray-500">
                <span className="text-4xl mb-2">✉️</span>
                <span className="text-sm">Aún no has enviado encuestas de satisfacción.</span>
              </div>
            ) : (
              encuestas.map((enc) => (
                <div
                  key={enc.id}
                  className="p-4 bg-white/5 rounded-xl border border-white/5 hover:border-white/10 transition-colors"
                >
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h3 className="font-semibold text-lg text-white">{enc.empresa}</h3>
                      <p className="text-xs text-gray-400">Por: {enc.nombre_estudiante}</p>
                    </div>
                    <div className="flex items-center space-x-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span
                          key={star}
                          className={star <= enc.calificacion ? "text-amber-400 text-lg" : "text-gray-700 text-lg"}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                  </div>
                  {enc.comentario && (
                    <p className="text-gray-300 text-sm border-t border-white/5 pt-2 mt-2">
                      {enc.comentario}
                    </p>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Encuestas;
