export const APP_ROUTES = {

  home: {
    home: ''
  },

  auth: {
    login: '/auth/login',
    register: '/auth/sign-up'
  },

  dash: {
    home: 'dashboard/home',
    patient: 'dashboard/patient'
  },

  analysis: {
    newAnalysis: 'dashboard/analysis/new-analysis',
    detailAnalysis: 'dashboard/analysis/detail-analyse'
  }

} as const;