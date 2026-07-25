describe('Flujo de Postulaciones', () => {
  beforeEach(() => {
    cy.login('estudiante@uce.edu.ec', 'password123')
  })

  it('ESTUDIANTE puede ver ofertas de pasantías', () => {
    cy.visit('/internships')
    cy.get('.lg\\:block').contains('Pasantías').should('be.visible')
  })

  it('Sección de postulaciones carga correctamente', () => {
    cy.visit('/internships')
    cy.get('body').should('not.contain', 'Error')
  })
})
