import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="dashboard-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-content">
          <h1><i class="fas fa-tachometer-alt"></i> Security Dashboard</h1>
          <p class="text-muted">Real-time security monitoring and analytics</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-outline-primary">
            <i class="fas fa-sync-alt"></i> Refresh
          </button>
          <button class="btn btn-primary">
            <i class="fas fa-download"></i> Export Report
          </button>
        </div>
      </div>

      <!-- Security Status Cards -->
      <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-4">
          <div class="card stat-card threat-level">
            <div class="card-body">
              <div class="stat-icon">
                <i class="fas fa-shield-alt"></i>
              </div>
              <div class="stat-content">
                <h3>LOW</h3>
                <p>Threat Level</p>
                <small class="text-success">
                  <i class="fas fa-arrow-down"></i> -15% from yesterday
                </small>
              </div>
            </div>
          </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
          <div class="card stat-card active-users">
            <div class="card-body">
              <div class="stat-icon">
                <i class="fas fa-users"></i>
              </div>
              <div class="stat-content">
                <h3>247</h3>
                <p>Active Users</p>
                <small class="text-info">
                  <i class="fas fa-arrow-up"></i> +5% from last week
                </small>
              </div>
            </div>
          </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
          <div class="card stat-card security-events">
            <div class="card-body">
              <div class="stat-icon">
                <i class="fas fa-exclamation-triangle"></i>
              </div>
              <div class="stat-content">
                <h3>12</h3>
                <p>Security Events</p>
                <small class="text-warning">
                  <i class="fas fa-arrow-up"></i> +3 in last hour
                </small>
              </div>
            </div>
          </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
          <div class="card stat-card failed-logins">
            <div class="card-body">
              <div class="stat-icon">
                <i class="fas fa-ban"></i>
              </div>
              <div class="stat-content">
                <h3>8</h3>
                <p>Failed Logins</p>
                <small class="text-danger">
                  <i class="fas fa-arrow-up"></i> +2 in last hour
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts and Analytics -->
      <div class="row mb-4">
        <div class="col-xl-8 mb-4">
          <div class="card">
            <div class="card-header">
              <h5><i class="fas fa-chart-line"></i> Security Activity Trends</h5>
            </div>
            <div class="card-body">
              <div class="chart-placeholder">
                <i class="fas fa-chart-line fa-3x text-muted"></i>
                <p class="text-muted mt-3">Activity trends chart will be displayed here</p>
              </div>
            </div>
          </div>
        </div>

        <div class="col-xl-4 mb-4">
          <div class="card">
            <div class="card-header">
              <h5><i class="fas fa-chart-pie"></i> Risk Distribution</h5>
            </div>
            <div class="card-body">
              <div class="chart-placeholder">
                <i class="fas fa-chart-pie fa-3x text-muted"></i>
                <p class="text-muted mt-3">Risk distribution chart</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activities and Alerts -->
      <div class="row">
        <div class="col-xl-6 mb-4">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5><i class="fas fa-clock"></i> Recent Activities</h5>
              <a routerLink="/audit" class="btn btn-sm btn-outline-primary">View All</a>
            </div>
            <div class="card-body">
              <div class="activity-list">
                <div class="activity-item">
                  <div class="activity-icon success">
                    <i class="fas fa-sign-in-alt"></i>
                  </div>
                  <div class="activity-content">
                    <p><strong>admin</strong> logged in successfully</p>
                    <small class="text-muted">2 minutes ago</small>
                  </div>
                </div>
                <div class="activity-item">
                  <div class="activity-icon warning">
                    <i class="fas fa-exclamation-triangle"></i>
                  </div>
                  <div class="activity-content">
                    <p>Failed login attempt from <strong>192.168.1.100</strong></p>
                    <small class="text-muted">5 minutes ago</small>
                  </div>
                </div>
                <div class="activity-item">
                  <div class="activity-icon info">
                    <i class="fas fa-user-plus"></i>
                  </div>
                  <div class="activity-content">
                    <p>New user <strong>john.doe</strong> created</p>
                    <small class="text-muted">15 minutes ago</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-xl-6 mb-4">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5><i class="fas fa-bell"></i> Security Alerts</h5>
              <a routerLink="/security" class="btn btn-sm btn-outline-primary">View All</a>
            </div>
            <div class="card-body">
              <div class="alert-list">
                <div class="alert-item high">
                  <div class="alert-icon">
                    <i class="fas fa-shield-alt"></i>
                  </div>
                  <div class="alert-content">
                    <p><strong>High Risk Activity Detected</strong></p>
                    <small>Multiple failed login attempts from suspicious IP</small>
                    <div class="alert-time">10 minutes ago</div>
                  </div>
                </div>
                <div class="alert-item medium">
                  <div class="alert-icon">
                    <i class="fas fa-user-shield"></i>
                  </div>
                  <div class="alert-content">
                    <p><strong>Unusual Access Pattern</strong></p>
                    <small>User accessing system outside normal hours</small>
                    <div class="alert-time">25 minutes ago</div>
                  </div>
                </div>
                <div class="alert-item low">
                  <div class="alert-icon">
                    <i class="fas fa-info-circle"></i>
                  </div>
                  <div class="alert-content">
                    <p><strong>System Update Available</strong></p>
                    <small>Security patch ready for installation</small>
                    <div class="alert-time">1 hour ago</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 0;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e9ecef;
    }

    .page-header h1 {
      color: #2c3e50;
      margin-bottom: 0.5rem;
      font-weight: 600;
    }

    .page-header h1 i {
      color: #667eea;
      margin-right: 0.5rem;
    }

    .header-actions {
      display: flex;
      gap: 0.5rem;
    }

    .stat-card {
      border: none;
      border-radius: 15px;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      overflow: hidden;
      position: relative;
    }

    .stat-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
    }

    .stat-card .card-body {
      padding: 1.5rem;
      display: flex;
      align-items: center;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: white;
      margin-right: 1rem;
    }

    .threat-level .stat-icon {
      background: linear-gradient(135deg, #28a745, #20c997);
    }

    .active-users .stat-icon {
      background: linear-gradient(135deg, #007bff, #6610f2);
    }

    .security-events .stat-icon {
      background: linear-gradient(135deg, #ffc107, #fd7e14);
    }

    .failed-logins .stat-icon {
      background: linear-gradient(135deg, #dc3545, #e83e8c);
    }

    .stat-content h3 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 0.25rem;
      color: #2c3e50;
    }

    .stat-content p {
      color: #6c757d;
      margin-bottom: 0.5rem;
      font-weight: 500;
    }

    .chart-placeholder {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #f8f9fa;
      border-radius: 10px;
    }

    .activity-list, .alert-list {
      max-height: 400px;
      overflow-y: auto;
    }

    .activity-item {
      display: flex;
      align-items: center;
      padding: 1rem 0;
      border-bottom: 1px solid #f1f3f4;
    }

    .activity-item:last-child {
      border-bottom: none;
    }

    .activity-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 1rem;
      color: white;
    }

    .activity-icon.success {
      background: #28a745;
    }

    .activity-icon.warning {
      background: #ffc107;
    }

    .activity-icon.info {
      background: #17a2b8;
    }

    .activity-content p {
      margin-bottom: 0.25rem;
      color: #2c3e50;
    }

    .alert-item {
      display: flex;
      align-items: flex-start;
      padding: 1rem;
      margin-bottom: 0.5rem;
      border-radius: 10px;
      border-left: 4px solid;
    }

    .alert-item.high {
      background: #fff5f5;
      border-left-color: #dc3545;
    }

    .alert-item.medium {
      background: #fff8e1;
      border-left-color: #ffc107;
    }

    .alert-item.low {
      background: #f0f8ff;
      border-left-color: #17a2b8;
    }

    .alert-icon {
      margin-right: 1rem;
      margin-top: 0.25rem;
    }

    .alert-content p {
      margin-bottom: 0.25rem;
      font-weight: 600;
    }

    .alert-content small {
      color: #6c757d;
    }

    .alert-time {
      font-size: 0.75rem;
      color: #6c757d;
      margin-top: 0.25rem;
    }

    .card {
      border: none;
      border-radius: 15px;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
    }

    .card-header {
      background: white;
      border-bottom: 1px solid #f1f3f4;
      border-radius: 15px 15px 0 0 !important;
      padding: 1.25rem 1.5rem;
    }

    .card-header h5 {
      margin: 0;
      color: #2c3e50;
      font-weight: 600;
    }

    .card-header h5 i {
      color: #667eea;
      margin-right: 0.5rem;
    }

    @media (max-width: 768px) {
      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .header-actions {
        width: 100%;
        justify-content: flex-end;
      }

      .stat-card .card-body {
        flex-direction: column;
        text-align: center;
      }

      .stat-icon {
        margin-right: 0;
        margin-bottom: 1rem;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
    // Initialize dashboard data
  }
}
