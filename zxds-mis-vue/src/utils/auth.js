export function isAuth(permission) {
	const permissions = localStorage.getItem('permissions')
	if (!permissions) return false
	for (const one of permission) {
		if (permissions.includes(one)) return true
	}
	return false
}