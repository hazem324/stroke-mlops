export const APP_ROUTES = {

  home: {
    home: ''
  },

  auth: {
    login: '/auth/login',
    register: '/auth/sign-up'
  },

  dash: {
    home: 'dashboard',
    patient: 'dashboard/patient'
  },

  analysis: {
    home: 'dashboard/analysis',
    newAnalysis: 'dashboard/analysis/new-analysis',
    detailAnalysis: 'dashboard/analysis/detail-analyse'
  }

} as const;