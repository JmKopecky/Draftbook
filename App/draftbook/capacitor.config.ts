import type {CapacitorConfig} from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.ionic.starter',
  appName: 'Draftbook',
  webDir: 'dist',
    plugins: {
      Keyboard: {
          resizeOnFullScreen: false
      },
      EdgeToEdge: {
        backgroundColor: "#101119"
      }
    }
};

export default config;
