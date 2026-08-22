export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: string;
  establishment: string;
  acceptedTerms: boolean;
}