import { WebView } from 'react-native-webview';
import { View, StyleSheet } from 'react-native';

const APP_URL = 'http://gisselamuzoqa1.distribuidauce.org';

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <WebView source={{ uri: APP_URL }} style={styles.webview} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  webview: { flex: 1 },
});
