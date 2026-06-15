import toast from 'react-hot-toast';

export function useAppToast() {
  return {
    success: toast.success,
    error: toast.error,
    loading: toast.loading,
    dismiss: toast.dismiss,
  };
}
