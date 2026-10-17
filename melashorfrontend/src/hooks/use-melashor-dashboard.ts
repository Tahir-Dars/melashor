import { useMemo, use } from "react";
import { getResourceOfDashboard } from "../lib/dashboardResource";
import type { DashboardData } from "../types";

export function useMelaShorDashboard(activeUserId: string) {

    const dashboardPromise = useMemo(() => getResourceOfDashboard(activeUserId), [activeUserId]);

    const initialData = use<DashboardData>(dashboardPromise);

    const activeUser =useMemo(
         ()=>initialData.users.find((user)=>user.userId===activeUserId ),
         []
    )

    return {
        users:initialData.users
    }
}