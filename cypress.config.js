const { defineConfig } = require('cypress')

module.exports = defineConfig({
  projectId: 'aatprk',
  e2e: {
    baseUrl: 'http://50.19.247.85',
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
