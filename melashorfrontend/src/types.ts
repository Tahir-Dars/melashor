export type UserProfile = {

    userId: string;
    handle: string;
    name: string;
    profileBio: string;
    hotUser: string;
}
export type FeedItem = {
    postId: string;
    authorId: string;
    authorName: string;
    authorHandle: string;
    content: string;
    createdAt: string;
    rankingScore: string;
    deliveryStrategy: string;
    rankingReason: string;
}

export type TimeLinePageResponse = {
    timelineOwnerId: string;
    feedItems: FeedItem[],
    mode: 'HOME' | 'USER' | string;
    totalItems: number,
    nextCursor: string | null;
}

export type PostResponse = {
    postId: string;
    authorId: string;
    authorName: string;
    authorHandle: string;
    content: string;
    createdAt: string;
}

export type followingResponse = {
    followerId: string;
    targetUserIds: string[];
    totalFollowing: number;
}
export type followResponse = {
    followerId: string;
    targetUserId: string;
    isFollowing: boolean;
    totalFollowing: number;
}

export type DashboardData = {
    users: UserProfile[];
    following: followingResponse;
    homefeed: TimeLinePageResponse;
    profileFeed: number;
}