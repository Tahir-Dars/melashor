import { useNavigate, useSearchParams } from "react-router-dom";
import { useMelaShorDashboard } from "../hooks/use-melashor-dashboard";
import { useState } from "react";

type AppShellProps = {
  activeUserId: string;
};

export function AppShell({ activeUserId }: AppShellProps) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const {
    users,
    activeUser,
    searchQuery,
    setSearchQuery,
    filteredUser,
    followingIds,
    selectedTimelineUserId,
    selectTimelineUser,
    handleFollowToggle,
  } = useMelaShorDashboard(activeUserId);

  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const [followBusyId, setFollowBusyId] = useState<string | null>(null);

  return (
    <div className="page-shell">
      <header className="topbar">
        <div className="brand">
          <h1>MelaShor</h1>
          <p>Social Feed</p>
        </div>

        <div className="topbar-actions">
          <label className="toolbar-select-label">
            Viewer
            <select
              className="toolbar-select"
              value={activeUserId}
              onChange={(event) => {
                const param = new URLSearchParams(searchParams);
                param.set("user", event.target.value);
                navigate({ search: param.toString() });
              }}
            >
              {users.map((user) => (
                <option key={user.userId} value={user.userId}>
                  {user.name}
                </option>
              ))}
            </select>
          </label>
        </div>
      </header>
    </div>
  );
}
