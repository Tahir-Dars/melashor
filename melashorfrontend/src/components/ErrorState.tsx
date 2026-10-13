type ErrorStateProps = {
  error: unknown;
};

export function ErrorState({ error }: ErrorStateProps) {
  const message = error instanceof Error ? error.message : "Unknown Error";

  return (
    <div className="error-shell">
      <div>
        <h1>Launch Interupted</h1>
        <p>{message}</p>
        <button type="button" onClick={() => window.location.reload}>
          Reload Experience
        </button>
      </div>
    </div>
  );
}
