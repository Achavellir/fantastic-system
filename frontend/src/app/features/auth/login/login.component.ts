import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <!-- Header -->
        <div class="login-header">
          <div class="logo">
            <i class="fas fa-shield-alt"></i>
          </div>
          <h1>SAMAP</h1>
          <p class="subtitle">Secure Access Management & Audit Platform</p>
        </div>

        <!-- Login Form -->
        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
          <!-- Username Field -->
          <div class="form-group">
            <label for="username">
              <i class="fas fa-user"></i>
              Username
            </label>
            <input
              type="text"
              id="username"
              formControlName="username"
              class="form-control"
              [class.is-invalid]="isFieldInvalid('username')"
              placeholder="Enter your username"
              autocomplete="username">
            <div class="invalid-feedback" *ngIf="isFieldInvalid('username')">
              <small *ngIf="loginForm.get('username')?.errors?.['required']">
                Username is required
              </small>
            </div>
          </div>

          <!-- Password Field -->
          <div class="form-group">
            <label for="password">
              <i class="fas fa-lock"></i>
              Password
            </label>
            <div class="password-input">
              <input
                [type]="showPassword ? 'text' : 'password'"
                id="password"
                formControlName="password"
                class="form-control"
                [class.is-invalid]="isFieldInvalid('password')"
                placeholder="Enter your password"
                autocomplete="current-password">
              <button
                type="button"
                class="password-toggle"
                (click)="togglePassword()"
                [attr.aria-label]="showPassword ? 'Hide password' : 'Show password'">
                <i [class]="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
            <div class="invalid-feedback" *ngIf="isFieldInvalid('password')">
              <small *ngIf="loginForm.get('password')?.errors?.['required']">
                Password is required
              </small>
            </div>
          </div>

          <!-- Remember Me -->
          <div class="form-check">
            <input
              type="checkbox"
              id="rememberMe"
              formControlName="rememberMe"
              class="form-check-input">
            <label for="rememberMe" class="form-check-label">
              Remember me
            </label>
          </div>

          <!-- Error Message -->
          <div class="alert alert-danger" *ngIf="errorMessage" role="alert">
            <i class="fas fa-exclamation-triangle"></i>
            {{ errorMessage }}
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            class="btn btn-primary btn-login"
            [disabled]="loginForm.invalid || isLoading">
            <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2"></span>
            <i *ngIf="!isLoading" class="fas fa-sign-in-alt me-2"></i>
            {{ isLoading ? 'Signing In...' : 'Sign In' }}
          </button>

          <!-- Forgot Password Link -->
          <div class="forgot-password">
            <a routerLink="/forgot-password">
              <i class="fas fa-question-circle"></i>
              Forgot your password?
            </a>
          </div>
        </form>

        <!-- Demo Credentials -->
        <div class="demo-credentials">
          <h6><i class="fas fa-info-circle"></i> Demo Credentials</h6>
          <div class="demo-accounts">
            <div class="demo-account" (click)="fillDemoCredentials('admin')">
              <strong>Admin:</strong> admin / admin123
            </div>
            <div class="demo-account" (click)="fillDemoCredentials('auditor')">
              <strong>Auditor:</strong> auditor / auditor123
            </div>
            <div class="demo-account" (click)="fillDemoCredentials('demo')">
              <strong>User:</strong> demo / demo123
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="login-footer">
          <p>&copy; 2024 SAMAP. Enterprise Security Solutions.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem;
    }

    .login-card {
      background: white;
      border-radius: 20px;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
      padding: 3rem;
      width: 100%;
      max-width: 450px;
      animation: slideUp 0.6s ease-out;
    }

    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .login-header {
      text-align: center;
      margin-bottom: 2.5rem;
    }

    .logo {
      width: 80px;
      height: 80px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1rem;
      color: white;
      font-size: 2rem;
    }

    .login-header h1 {
      font-size: 2.5rem;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 0.5rem;
      letter-spacing: 2px;
    }

    .subtitle {
      color: #6c757d;
      font-size: 0.95rem;
      margin-bottom: 0;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .form-group label i {
      margin-right: 0.5rem;
      color: #667eea;
    }

    .form-control {
      width: 100%;
      padding: 0.875rem 1rem;
      border: 2px solid #e9ecef;
      border-radius: 10px;
      font-size: 1rem;
      transition: all 0.3s ease;
    }

    .form-control:focus {
      border-color: #667eea;
      box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
      outline: none;
    }

    .form-control.is-invalid {
      border-color: #dc3545;
    }

    .password-input {
      position: relative;
    }

    .password-toggle {
      position: absolute;
      right: 1rem;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      color: #6c757d;
      cursor: pointer;
      padding: 0;
    }

    .password-toggle:hover {
      color: #667eea;
    }

    .form-check {
      margin-bottom: 1.5rem;
    }

    .form-check-input {
      margin-right: 0.5rem;
    }

    .form-check-label {
      color: #6c757d;
      font-size: 0.9rem;
    }

    .btn-login {
      width: 100%;
      padding: 0.875rem;
      font-size: 1.1rem;
      font-weight: 600;
      border-radius: 10px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      transition: all 0.3s ease;
    }

    .btn-login:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
    }

    .btn-login:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .forgot-password {
      text-align: center;
      margin-top: 1.5rem;
    }

    .forgot-password a {
      color: #667eea;
      text-decoration: none;
      font-size: 0.9rem;
    }

    .forgot-password a:hover {
      text-decoration: underline;
    }

    .demo-credentials {
      margin-top: 2rem;
      padding: 1.5rem;
      background: #f8f9fa;
      border-radius: 10px;
      border: 1px solid #e9ecef;
    }

    .demo-credentials h6 {
      color: #495057;
      margin-bottom: 1rem;
      font-weight: 600;
    }

    .demo-credentials h6 i {
      color: #17a2b8;
      margin-right: 0.5rem;
    }

    .demo-account {
      padding: 0.5rem;
      margin-bottom: 0.5rem;
      background: white;
      border-radius: 5px;
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 0.85rem;
    }

    .demo-account:hover {
      background: #e9ecef;
      transform: translateX(5px);
    }

    .demo-account:last-child {
      margin-bottom: 0;
    }

    .login-footer {
      text-align: center;
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid #e9ecef;
    }

    .login-footer p {
      color: #6c757d;
      font-size: 0.8rem;
      margin: 0;
    }

    .alert {
      border-radius: 10px;
      margin-bottom: 1.5rem;
    }

    .invalid-feedback {
      display: block;
      margin-top: 0.25rem;
    }

    @media (max-width: 576px) {
      .login-container {
        padding: 1rem;
      }

      .login-card {
        padding: 2rem;
      }

      .login-header h1 {
        font-size: 2rem;
      }
    }
  `]
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
    // Clear any existing error messages
    this.errorMessage = '';
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const credentials = {
        username: this.loginForm.value.username,
        password: this.loginForm.value.password
      };

      this.authService.login(credentials).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Invalid username or password';
        }
      });
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  fillDemoCredentials(type: 'admin' | 'auditor' | 'demo'): void {
    const credentials = {
      admin: { username: 'admin', password: 'admin123' },
      auditor: { username: 'auditor', password: 'auditor123' },
      demo: { username: 'demo', password: 'demo123' }
    };

    this.loginForm.patchValue(credentials[type]);
  }
}
