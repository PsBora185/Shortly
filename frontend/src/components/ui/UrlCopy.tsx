import { Check, Copy } from 'lucide-react';
import { useState } from 'react';
import { Button } from './Button';

export function UrlCopy({ value }: { value: string }) {
  const [copied, setCopied] = useState(false);

  async function copyValue() {
    await navigator.clipboard.writeText(value);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1500);
  }

  return (
    <Button type="button" variant="ghost" onClick={copyValue} className="gap-2">
      {copied ? <Check className="h-4 w-4 text-emerald-600" /> : <Copy className="h-4 w-4" />}
      {copied ? 'Copied' : 'Copy'}
    </Button>
  );
}
