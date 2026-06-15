import { useMemo } from 'react';
import { BarChart3, MousePointerClick, TrendingUp } from 'lucide-react';
import { AnalyticsCharts } from '../components/analytics/AnalyticsCharts';
import { StatCard } from '../components/analytics/StatCard';
import { Card } from '../components/ui/Card';
import { useUrls } from '../hooks/useUrls';
import { formatShortDate } from '../utils/format';

export function AnalyticsPage() {
  const { data: urls = [], isLoading } = useUrls();

  const stats = useMemo(() => {
    const totalClicks = urls.reduce((sum, url) => sum + url.clicks, 0);
    const totalUrls = urls.length;
    const averageClicks = totalUrls ? totalClicks / totalUrls : 0;
    return { totalClicks, totalUrls, averageClicks };
  }, [urls]);

  const clickTrend = useMemo(
    () =>
      [...urls]
        .sort((a, b) => +new Date(a.createdAt) - +new Date(b.createdAt))
        .slice(0, 7)
        .map((url) => ({ name: formatShortDate(url.createdAt), clicks: url.clicks })),
    [urls],
  );

  const topUrls = useMemo(
    () =>
      [...urls]
        .sort((a, b) => b.clicks - a.clicks)
        .slice(0, 6)
        .map((url) => ({ name: url.shortCode, clicks: url.clicks })),
    [urls],
  );

  return (
    <div className="space-y-6">
      <section className="grid gap-4 md:grid-cols-3">
        <StatCard label="Total URLs" value={stats.totalUrls} delta="+12% this week" />
        <StatCard label="Total Clicks" value={stats.totalClicks} delta="+8% this week" />
        <StatCard label="Average Clicks" value={stats.averageClicks.toFixed(1)} delta="Stable" />
      </section>

      <AnalyticsCharts clickTrend={clickTrend} topUrls={topUrls} />

      <Card>
        <h3 className="flex items-center gap-2 text-lg font-semibold text-slate-900">
          <TrendingUp className="h-5 w-5 text-brand-600" />
          Most clicked links
        </h3>
        <div className="mt-4 grid gap-3">
          {topUrls.map((item) => (
            <div key={item.name} className="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
              <div>
                <p className="font-medium text-slate-900">{item.name}</p>
                <p className="text-sm text-slate-500">Top performing URL</p>
              </div>
              <div className="flex items-center gap-2 text-sm font-semibold text-slate-900">
                <MousePointerClick className="h-4 w-4 text-brand-600" />
                {item.clicks}
              </div>
            </div>
          ))}
        </div>
      </Card>

      <Card>
        <h3 className="flex items-center gap-2 text-lg font-semibold text-slate-900">
          <BarChart3 className="h-5 w-5 text-brand-600" />
          Recent activity
        </h3>
        <div className="mt-4 space-y-3">
          {isLoading ? (
            <p className="text-sm text-slate-500">Loading analytics...</p>
          ) : (
            urls.slice(0, 5).map((url) => (
              <div key={url.id} className="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
                <div>
                  <p className="font-medium text-slate-900">{url.shortUrl}</p>
                  <p className="text-sm text-slate-500">{formatShortDate(url.createdAt)}</p>
                </div>
                <span className="text-sm font-semibold text-slate-900">{url.clicks} clicks</span>
              </div>
            ))
          )}
        </div>
      </Card>
    </div>
  );
}
