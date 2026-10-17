import {
    type TimeLinePageResponse, type followingResponse, type followResponse,
    type UserProfile, type PostResponse, type DashboardData
} from "../types";

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
        (`/follows?followerId=${encodeURIComponent(followerId)}`),

    getHomeFeed: (userId: string, cursor?: null) => {
        const param = new URLSearchParams({ userId, limit: '5' });

        if (cursor) {
            param.set('cursor', cursor);
        }
        return request<TimeLinePageResponse>(`/feed/home?${param.toString}`);
    },

    getUserFeed: (userId: string, cursor?: string | null) => {
        const param = new URLSearchParams({ userId, limit: '5' });

        if (cursor) {
            param.set('cursor', cursor);
        }
        return request<TimeLinePageResponse>
            (`/feed/user${encodeURIComponent(userId)}?${param.toString}`);


    },
    createPost: (authorId: string, content: string) => request<PostResponse>(
        '/posts',
        {
            method: 'POST',
            body: JSON.stringify({
                idempotencyKey: createIdempotencyKey(),
                authorId,
                content
            })
        }),

    followUser: (followerId: string, targetUserId: string) =>
        request<followResponse>(
            `/follow/${encodeURIComponent(targetUserId)}?followerId=${encodeURIComponent(followerId)}`,
            { method: 'POST' }
        ),
    unFollowUser: (followerId: string, targetUserId: string) => request<followResponse>(
        `/follow/${encodeURIComponent(targetUserId)}?followerId=${encodeURIComponent(followerId)}`,
        { method: 'DELETE' }
    ),

    async getDahsboardData(activeUserId: string): Promise<DashboardData> {
        const [users, following, homefeed, profileFeed] = await Promise.all(
            [
                this.getUsers(),
                this.getFollowing(activeUserId),
                this.getHomeFeed(activeUserId),
                this.getUserFeed(activeUserId),
            ]
        )
        return { users, following, homefeed, profileFeed };
    }
}
function createIdempotencyKey() {
    if (typeof globalThis.crypto !== 'undefined' &&
        typeof globalThis.crypto.randomUUID === 'function') {

        return globalThis.crypto.randomUUID();
    }

    return `melashor-${Date.now}-${Math.random().toString(36).slice(2, 12)}`;
}

