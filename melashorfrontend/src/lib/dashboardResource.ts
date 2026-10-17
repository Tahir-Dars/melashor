import type { DashboardData } from "../types"
import { feedApi } from "./api";

const cache = new Map<String, Promise<DashboardData>>();


export function getResourceOfDashboard(activeUserId: string): Promise<DashboardData> {
    const existing = cache.get(activeUserId);
    if (existing) {
        return existing;
    }

    const promise = feedApi.getDahsboardData(activeUserId).catch((error) => {
        cache.delete(activeUserId);
        throw error;
    });

    cache.set(activeUserId, promise);
    return promise;
}

export function invalidateDashboardResource(activeUserId: string) {
    cache.delete(activeUserId);
}