const key = 'pulse.accessToken'
export const getAccessToken = () => sessionStorage.getItem(key)
export const saveAccessToken = (token: string) => sessionStorage.setItem(key, token)
export const clearAccessToken = () => sessionStorage.removeItem(key)
