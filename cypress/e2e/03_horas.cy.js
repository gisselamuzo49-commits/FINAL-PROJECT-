describe('Flujo de Horas', () => {
  it('ESTUDIANTE puede ver sección de horas', () => {
    cy.login('estudiante@uce.edu.ec', 'password123')
    cy.visit('/hours')
    cy.contains('Horas').should('be.visible')
  })

  it('TUTOR puede ver sección de validación de horas', () => {
    cy.login('tutor@uce.edu.ec', 'password123')
    cy.visit('/hours')
    cy.contains('Validar').should('be.visible')
  })
})
