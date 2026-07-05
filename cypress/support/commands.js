Cypress.Commands.add('login', (email, password) => {
  cy.visit('/login')
  cy.get('input[type="email"], input[name="email"]').type(email)
  cy.get('input[type="password"]').type(password)
  cy.get('button[type="submit"]').click()
  cy.url().should('include', '/home')
})
