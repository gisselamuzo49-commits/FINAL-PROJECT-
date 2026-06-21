import AuthLayout from '../components/auth/AuthLayout';

function CheckEmail() {
  const handleOpenMailbox = () => {
    // TODO: eventualmente abriría mailto: o el cliente de correo
    console.log('Opening mailbox...');
  };

  return (
    <AuthLayout>
      <div className="flex flex-col items-center">
        {/* Envelope Icon */}
        <div className="text-4xl mb-4">✉️</div>
        
        <h2 className="text-2xl font-bold text-white mb-2 text-center">¡Revisa tu email!</h2>
        <p className="text-sm text-gray-300 text-center mb-6 leading-relaxed">
          Hemos enviado un enlace de verificación a tu correo institucional.
          Por favor, revisa tu bandeja de entrada o spam.
        </p>

        <button
          onClick={handleOpenMailbox}
          className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white rounded-lg py-3 w-full font-semibold transition shadow-md focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] mb-6"
        >
          Open mail box
        </button>

        <div className="text-center">
          <button
            onClick={() => console.log('Reenviando email...')}
            className="text-sm text-gray-300 hover:text-white transition duration-200 underline focus:outline-none"
          >
            &lt; Reenviar email
          </button>
        </div>
      </div>
    </AuthLayout>
  );
}

export default CheckEmail;
