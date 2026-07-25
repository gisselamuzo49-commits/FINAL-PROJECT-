const { defineConfig } = require('cypress')

module.exports = defineConfig({
  projectId: 'aatprk',
  e2e: {
    baseUrl: 'http://54.227.79.26',
    viewportWidth: 1280,
    viewportHeight: 720,
    specPattern: 'cypress/e2e/**/*.cy.js',
    screenshotsFolder: 'cypress/screenshots',
    videosFolder: 'cypress/videos',
    video: true,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    pageLoadTimeout: 30000,
    setupNodeEvents(on, config) {
      return config
    },
  },
})
