export default function Loading() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="flex flex-col items-center gap-4">
        <div className="w-14 h-14 rounded-full border-4 border-green-200 border-t-green-600 animate-spin" />
        <p className="text-gray-500 text-sm">Loading NearKart…</p>
      </div>
    </div>
  );
}
