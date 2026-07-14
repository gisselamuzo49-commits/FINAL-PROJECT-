describe('Flujo de Postulaciones', () => {
  beforeEach(() => {
    cy.login('estudiante@uce.edu.ec', Cypress.env('TEST_PASSWORD'))
  })

  it('ESTUDIANTE puede ver ofertas de pasantías', () => {
    cy.visit('/internships')
    cy.contains('Pasantías').should('be.visible')
  })

  it('Sección de postulaciones carga correctamente', () => {
    cy.visit('/internships')
    cy.get('body').should('not.contain', 'Error')
  })
})
