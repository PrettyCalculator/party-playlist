document.addEventListener("DOMContentLoaded", function () {
    const nextForm = document.getElementById("next-form");
    if (nextForm === null) {
        return;
    }

    const audio = document.getElementById("player");
    if (audio !== null) {
        audio.addEventListener("ended", function () {
            nextForm.submit();
        });
    }

    const youtube = document.getElementById("youtube-player");
    if (youtube !== null) {
        window.onYouTubeIframeAPIReady = function () {
            new YT.Player("youtube-player", {
                videoId: youtube.dataset.videoId,
                playerVars: { autoplay: 1 },
                events: {
                    onStateChange: function (event) {
                        if (event.data === YT.PlayerState.ENDED) {
                            nextForm.submit();
                        }
                    }
                }
            });
        };

        const script = document.createElement("script");
        script.src = "https://www.youtube.com/iframe_api";
        document.head.appendChild(script);
    }
});
