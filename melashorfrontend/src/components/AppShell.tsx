import { useNavigate, useSearchParams } from "react-router-dom";
import { useMelaShorDashboard } from "../hooks/use-melashor-dashboard";
import { useState } from "react";
import { FiSearch } from "react-icons/fi";

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

  const followedUsers = filteredUser.filter(
    (user) =>
      user.userId !== activeUserId && followingIds.includes(user.userId),
  );

  const suggestedUsers = filteredUser.filter(
    (user) =>
      user.userId !== activeUserId && !followingIds.includes(user.userId),
  );

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
      <main className="melashor-workspace three-column">
        <section className="column-stack">
          <section className="panel roster-panel">
            <div className="panel-header panel-header-search">
              <div className="panel-title-with-search">
                <p className="head">Following</p>
                <div className="panel-title-row">
                  <h2>{activeUser.name}</h2>
                  <button
                    aria-expanded={isSearchOpen}
                    aria-label={isSearchOpen ? "Hide Search" : "Show search"}
                    className="icon-button"
                    type="button"
                    onClick={() => setIsSearchOpen((previous) => !previous)}
                  >
                    <FiSearch />
                  </button>
                </div>
                {!isSearchOpen ? (
                  <input
                    aria-label="search people"
                    className="search-input"
                    placeholder="Search"
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                  />
                ) : null}
              </div>
            </div>
            <div className="user-list">
              {/* {followedUsers.length > 0 ? (
                renderUserCards("followed")
              ) : (
                <p className="empty-text">No Followed Users Yet</p>
              )} */}
            </div>
          </section>
        </section>
      </main>
    </div>
  );
}
