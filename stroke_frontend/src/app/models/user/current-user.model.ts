export interface CurrentUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string | null;
  establishment: string | null;
}