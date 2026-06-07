package com.vspace.bean

/**
 * Data class representing Google Mobile Services (GMS) installation status for a virtual user.
 *
 * @property userID the virtual user ID.
 * @property userName the display name or remark for the user.
 * @property isInstalledGms whether GMS is currently installed for this user.
 */
data class GmsBean(
	val userID: Int,
	val userName: String,
	var isInstalledGms: Boolean
)

/**
 * Data class representing the result of a GMS install/uninstall operation.
 *
 * @property userID the virtual user ID the operation targeted.
 * @property success whether the operation completed successfully.
 * @property msg a human-readable result message.
 */
data class GmsInstallBean(
	val userID: Int,
	val success: Boolean,
	val msg: String
)
