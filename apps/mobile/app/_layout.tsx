import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';

export default function RootLayout() {
  return (
    <>
      <StatusBar style="light" backgroundColor="#0a1628" />
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: '#0a1628' },
          headerTintColor: '#FFD700',
          headerTitleStyle: { fontWeight: 'bold' },
        }}
      >
        <Stack.Screen 
          name="index" 
          options={{ title: 'Pasantías UCE', headerShown: false }} 
        />
      </Stack>
    </>
  );
}
