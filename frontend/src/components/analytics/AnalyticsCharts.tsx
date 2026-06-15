import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card } from '../ui/Card';

export function AnalyticsCharts({
  clickTrend,
  topUrls,
}: {
  clickTrend: Array<{ name: string; clicks: number }>;
  topUrls: Array<{ name: string; clicks: number }>;
}) {
  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <Card>
        <h3 className="text-lg font-semibold text-slate-900">Click Trend</h3>
        <p className="mt-1 text-sm text-slate-500">Clicks grouped by creation date.</p>
        <div className="mt-6 h-80">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={clickTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="name" stroke="#64748b" />
              <YAxis stroke="#64748b" />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="clicks" stroke="#6470ff" strokeWidth={3} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </Card>

      <Card>
        <h3 className="text-lg font-semibold text-slate-900">Top URLs</h3>
        <p className="mt-1 text-sm text-slate-500">Highest performing short links.</p>
        <div className="mt-6 h-80">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={topUrls}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="name" stroke="#64748b" />
              <YAxis stroke="#64748b" />
              <Tooltip />
              <Legend />
              <Bar dataKey="clicks" fill="#0f172a" radius={[12, 12, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>
    </div>
  );
}
