import { format, formatDistanceToNow } from 'date-fns';

export function formatDateTime(value?: string | null) {
  if (!value) {
    return 'N/A';
  }
  return format(new Date(value), 'MMM d, yyyy • h:mm a');
}

export function formatShortDate(value?: string | null) {
  if (!value) {
    return 'N/A';
  }
  return format(new Date(value), 'MMM d');
}

export function formatRelative(value?: string | null) {
  if (!value) {
    return 'N/A';
  }
  return formatDistanceToNow(new Date(value), { addSuffix: true });
}
