import type { UrlResponse } from '../types';

export const mockUrls: UrlResponse[] = [
  {
    id: 'a1',
    originalUrl: 'https://spring.io/projects/spring-boot',
    shortCode: 'boot2026',
    shortUrl: 'http://localhost:8080/boot2026',
    createdAt: new Date().toISOString(),
    clicks: 128,
    lastAccessed: new Date().toISOString(),
  },
  {
    id: 'a2',
    originalUrl: 'https://react.dev/',
    shortCode: 'reactx',
    shortUrl: 'http://localhost:8080/reactx',
    createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
    clicks: 74,
    lastAccessed: new Date(Date.now() - 3600000).toISOString(),
  },
  {
    id: 'a3',
    originalUrl: 'https://www.docker.com/',
    shortCode: 'dockit',
    shortUrl: 'http://localhost:8080/dockit',
    createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
    clicks: 41,
    lastAccessed: new Date(Date.now() - 86400000).toISOString(),
  },
];
