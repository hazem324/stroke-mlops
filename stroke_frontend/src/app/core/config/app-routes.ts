export const APP_ROUTES = {

  home :{ 
    home: ''
  },

  auth: {
    login: '/auth/login',
    register: '/auth/sign-up'
  },

  dash : {
    home: 'dashboard',
    patient: 'dashboard/patient'
  }

} as const;