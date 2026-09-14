package com.example.fila_virtual.repository

object EmployeeInvitationEmailTemplate {
    // Para pruebas locales usa la IP de tu PC en la misma red que el teléfono.
    private const val INVITATION_BASE_URL = "http://192.168.100.20:8080/invitacion/"

    fun create(
        token: String,
        email: String,
        ownerNombre: String,
        establecimientoNombre: String,
        rol: String
    ): MailDocument {
        val invitationLink = "$INVITATION_BASE_URL?token=$token"
        return MailDocument(
            to = email,
            message = MailMessage(
                subject = "Invitación para unirte a $establecimientoNombre - AlToque",
                text = "$ownerNombre te invitó a unirte a $establecimientoNombre como $rol. Acepta la invitación desde: $invitationLink. Caduca en 24 horas.",
                html = """
                    <div style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
                        <h2 style="color: #333;">Invitación para unirte a $establecimientoNombre</h2>
                        <p style="color: #555;"><strong>$ownerNombre</strong> te invitó a participar como <strong>$rol</strong>.</p>
                        <p style="color: #555;">Confirma tu identidad y acepta la invitación desde la aplicación:</p>
                        <a href="$invitationLink" style="background-color: #FF5A36; color: white; padding: 14px 24px; border-radius: 8px; display: inline-block; text-decoration: none; font-weight: bold; margin: 12px 0;">Aceptar invitación</a>
                        <p style="color: #999; font-size: 12px;">La invitación caduca en 24 horas. Si no esperabas este correo, puedes ignorarlo.</p>
                    </div>
                """.trimIndent()
            )
        )
    }
}
