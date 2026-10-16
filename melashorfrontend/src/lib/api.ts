const API_ROOT = '/api';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${API_ROOT}${path}`,
        {
            ...init,
            headers: {
                'Content-Type': 'application/json',
                ... (init?.headers ?? {})
            }
        }
    );

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `Request failed with status ${response.status}`)
    }

}