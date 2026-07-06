import { WebView } from 'react-native-webview';
import { StyleSheet, View, ActivityIndicator, Text } from 'react-native';
import { useState } from 'react';

const QA_URL = 'http://gisselamuzoqa1.distribuidauce.org';
const PROD_URL = 'http://gissleamuzoprod1.distribuidauce.org';
const APP_URL = process.env.EXPO_PUBLIC_APP_ENV === 'prod' ? PROD_URL : QA_URL;

export default function HomeScreen() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorIcon}>📡</Text>
        <Text style={styles.errorTitle}>Sin conexión</Text>
        <Text style={styles.errorText}>
          No se puede conectar al servidor.{'\n'}
          Verifica tu conexión a internet.
        </Text>
        <Text style={styles.footer}>
          Universidad Central del Ecuador{'\n'}
          FICA - Programación Distribuida 2026
        </Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {loading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#FFD700" />
          <Text style={styles.loadingText}>
            Cargando Sistema de Pasantías UCE...
          </Text>
        </View>
      )}
      <WebView
        source={{ uri: APP_URL }}
        style={styles.webview}
        onLoadEnd={() => setLoading(false)}
        onError={() => { setLoading(false); setError(true); }}
        onHttpError={() => { setLoading(false); setError(true); }}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        startInLoadingState={true}
        allowsInlineMediaPlayback={true}
        mediaPlaybackRequiresUserAction={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0a1628' },
  webview: { flex: 1 },
  loadingContainer: {
    position: 'absolute',
    top: 0, left: 0, right: 0, bottom: 0,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#0a1628',
    zIndex: 1,
  },
  loadingText: {
    color: '#FFD700',
    marginTop: 16,
    fontSize: 14,
    textAlign: 'center',
  },
  errorContainer: {
    flex: 1,
    backgroundColor: '#0a1628',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  errorIcon: { fontSize: 64, marginBottom: 16 },
  errorTitle: {
    color: '#FF6B6B',
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  errorText: {
    color: '#ccc',
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 32,
  },
  footer: {
    color: '#666',
    fontSize: 12,
    textAlign: 'center',
    position: 'absolute',
    bottom: 32,
  },
});
