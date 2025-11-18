export interface TestConfig {
  serverUrl: string;
}

export const config: TestConfig = {
  serverUrl: process.env.CBIOPORTAL_URL || 'http://localhost:8080'
};