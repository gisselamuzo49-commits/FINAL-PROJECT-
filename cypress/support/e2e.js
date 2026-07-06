Cypress.on('uncaught:exception', (err) => {
  // Ignorar errores de JS no críticos
  return false
})
