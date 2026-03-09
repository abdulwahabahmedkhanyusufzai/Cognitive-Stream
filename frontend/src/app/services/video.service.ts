import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class VideoService {
  private http = inject(HttpClient);
  
  // Pointing to your Azure Backend
  private readonly API_BASE = 'https://ba-backend-v2.jollyfield-759e8655.uaenorth.azurecontainerapps.io/api/v1';

  /**
   * Fetches HLS stream metadata from your Java Backend.
   */
  getStreamUrl(type: string, id: string): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(`${this.API_BASE}/stream/${type}/${id}`);
  }

  /**
   * Fetches the official trailers for the content.
   */
  getTrailers(type: string, id: string): Observable<{ trailers: any[] }> {
    return this.http.get<{ trailers: any[] }>(`${this.API_BASE}/${type}/${id}/trailers`);
  }

  /**
   * Fetches similar content recommendations.
   */
  getSimilar(type: string, id: string): Observable<{ similar: any[] }> {
    return this.http.get<{ similar: any[] }>(`${this.API_BASE}/${type}/${id}/similar`);
  }

  /**
   * Fetches full metadata (plot, cast, rating) for the detail page.
   */
  getDetails(type: string, id: string): Observable<{ content: any }> {
    return this.http.get<{ content: any }>(`${this.API_BASE}/${type}/${id}/details`);
  }
}