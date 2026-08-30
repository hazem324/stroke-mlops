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
    patient: 'dashboard/patient',
    history : 'dashboard/analysis/analysis-history'
  },

  analysis: {
    newAnalysis: 'dashboard/analysis/new-analysis',
    detailAnalysis: 'dashboard/analysis/detail-analyse'
  }

} as const;