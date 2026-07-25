import React, { useState, useRef } from 'react';
import { WebView } from 'react-native-webview';
import { View, StyleSheet } from 'react-native';
import OfflineScreen from './offline';

const APP_URL = 'http://gisselamuzoqa1.distribuidauce.org';

export default function HomeScreen() {
  const [hasError, setHasError] = useState(false);
  const webViewRef = useRef<WebView>(null);

  const handleRetry = () => {
    setHasError(false);
    if (webViewRef.current) {
      webViewRef.current.reload();
    }
  };

  if (hasError) {
    return <OfflineScreen onRetry={handleRetry} />;
  }

  return (
    <View style={styles.container}>
      <WebView
        ref={webViewRef}
        source={{ uri: APP_URL }}
        style={styles.webview}
        originWhitelist={["*"]}
        mixedContentMode="always"
        onError={() => setHasError(true)}
        onHttpError={() => setHasError(true)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  webview: { flex: 1 },
});
