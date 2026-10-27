import { useMemo, useDeferredValue, use, useState } from "react";
import { getResourceOfDashboard } from "../lib/dashboardResource";
import type { DashboardData, followResponse, UserProfile } from "../types";
import { feedApi } from "../lib/api";

export function useMelaShorDashboard(activeUserId: string) {

    const dashboardPromise = useMemo(() => getResourceOfDashboard(activeUserId), [activeUserId]);

    const initialData = use<DashboardData>(dashboardPromise);

    const [searchQuery, setSearchQuery] = useState('');
    const [followingIds, setFollowingIds] =
        useState<string[]>(initialData.following.targetUserIds);
    const [selectedTimelineUserId, setSelectedTimelineUserId] =
        useState<string>(initialData.following.targetUserIds[0] ?? '');

    const activeUser = useMemo(
        () => initialData.users.find((user) => user.userId === activeUserId)
            ?? initialData.users[0],
        [activeUserId, initialData.users])

    const deferredSearchQuery = useDeferredValue(searchQuery);

    const filteredUser = useMemo(() => {
        const query = deferredSearchQuery.trim().toLowerCase();
        if (!query) {
            return initialData.users;
        }
        return initialData.users.filter(
            (user) => {
                let filterby = `${user.name}${user.handle}${user.profileBio}`;
                return filterby.toLowerCase().includes(query);
            })

    }, [deferredSearchQuery, initialData])


    function selectTimelineUser(user: UserProfile) {
        setSelectedTimelineUserId(user.userId);
    }
    async function handleFollowToggle(
        targetUserId: string
    ): Promise<followResponse> {
        const currentlyFollowing = followingIds.includes(targetUserId);
        const response = currentlyFollowing ?
            await feedApi.unFollowUser(activeUserId, targetUserId) :
            await feedApi.followUser(activeUserId, targetUserId);

        setFollowingIds((previous) => currentlyFollowing ? previous.filter((id) =>
            id !== targetUserId) : [...previous, targetUserId]);


        return response;
    }

    return {
        users: initialData.users,
        activeUser,
        searchQuery,
        setSearchQuery,
        filteredUser,
        followingIds,
        selectedTimelineUserId,
        selectTimelineUser,
        handleFollowToggle
    }
}