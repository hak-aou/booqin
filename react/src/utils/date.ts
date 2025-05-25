export const formatDate = (isoString: string | undefined) => {
    if (!isoString) return "";
    const date = new Date(isoString);
    return new Intl.DateTimeFormat(navigator.language, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    }).format(date);
};

export const formatDateWithTime = (isoString: string | undefined) => {
    if (!isoString) return "";
    const date = new Date(isoString);
    return new Intl.DateTimeFormat(navigator.language, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

// Display the date in a human-readable format, with a hint of how long ago it was
export const formatDateWithHint = (isoString: string | undefined) => {
    if (!isoString) return "";
    const date = new Date(isoString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    if (diff < 1000 * 60) {
        return "Just now";
    } else if (diff < 1000 * 60 * 60) {
        return `${Math.floor(diff / (1000 * 60))} minutes ago`;
    } else if (diff < 1000 * 60 * 60 * 24) {
        return `${Math.floor(diff / (1000 * 60 * 60))} hours ago`;
    } else if (diff < 1000 * 60 * 60 * 24 * 7) {
        return `${Math.floor(diff / (1000 * 60 * 60 * 24))} days ago`;
    } else {
        return new Intl.DateTimeFormat(navigator.language, {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        }).format(date);
    }
}

export const formatTimeLeft = (isoString: string | undefined) => {
    if (!isoString) return "";
    const lockDate = new Date(isoString);
    const now = new Date();
    const diff = lockDate.getTime() - now.getTime();

    if (diff <= 0) {
        return "";
    }

    const totalSeconds = Math.floor(diff / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
};