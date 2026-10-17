import { TimeLinePageResponse, type followingResponse, type followResponse, type UserProfile } from "../types";

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
        throw new Error(message || `Request failed with status ${response.status}`);
    }

    return response.json() as Promise<T>;

}


export const feedApi = {
    getUsers: () => request<UserProfile[]>('/users'),

    getFollowing: (followerId: string) => request<followingResponse>
        (`/follows?followerId=${followerId}`),

    getHomeFeed: (userId: string, cursor?: null) => {
        const param = new URLSearchParams({ userId, limit: '5' });

        if (cursor) {
            param.set('cursor', cursor);
        }
        return request<TimeLinePageResponse>(`/feed/home?${param.toString}`);
    }
    }
     getUserFeed: (userId: string, cursor?: string | null) => {
        const param = new URLSearchParams({ userId, limit: '5' });

        if (cursor) {
            param.set('cursor', cursor);
        }
        return request<TimeLinePageResponse>(`/feed/home?${param.toString}`);

}