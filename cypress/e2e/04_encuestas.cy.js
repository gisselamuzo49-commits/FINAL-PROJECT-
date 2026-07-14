describe('Flujo de Encuestas Supabase', () => {
  beforeEach(() => {
    cy.login('estudiante@uce.edu.ec', Cypress.env('TEST_PASSWORD'))
  })

  it('ESTUDIANTE puede ver sección de encuestas', () => {
    cy.visit('/encuestas')
    cy.contains('Encuestas').should('be.visible')
  })

  it('Formulario de encuesta carga sin error', () => {
    cy.visit('/encuestas')
    cy.get('body').should('not.contain', 'Error de configuración')
  })
})
