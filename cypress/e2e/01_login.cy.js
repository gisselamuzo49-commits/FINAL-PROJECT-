describe('Login y navegación', () => {
  it('ESTUDIANTE puede hacer login', () => {
    cy.visit('/login')
    cy.get('input[type="email"]').type('estudiante@uce.edu.ec')
    cy.get('input[type="password"]').type(Cypress.env('TEST_PASSWORD'), { log: false })
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/home')
    cy.contains('Mis Postulaciones').should('be.visible')
  })

  it('TUTOR puede hacer login', () => {
    cy.visit('/login')
    cy.get('input[type="email"]').type('tutor@uce.edu.ec')
    cy.get('input[type="password"]').type(Cypress.env('TEST_PASSWORD'), { log: false })
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/home')
  })
})
