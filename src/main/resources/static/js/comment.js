const articleId = document.getElementById('article-id').value;


const deleteButtons = Array.prototype.slice.call(
    document.getElementsByClassName('comment-delete-btn')
);

deleteButtons.forEach((deleteButton, index, array) => {
    if (deleteButton) {
        deleteButton.addEventListener('click', event => {
            let id = document.getElementById('comment' + index).value;
            function success() {
                alert('삭제가 완료되었습니다.');
                location.replace(`/board/${articleId}`);
            }

            function fail() {
                alert('삭제 실패했습니다.');
                location.replace(`/board/${articleId}`);
            }

            httpRequest('DELETE',`/api/articles/${articleId}/comments/${id}`, null, success, fail);
        });
    }
});


const commentCreateButton = document.getElementById('comment-create-btn');

if (commentCreateButton) {
    commentCreateButton.addEventListener('click', event => {
        body = JSON.stringify({
            content: document.getElementById('textarea-comment').value
        });
        function success() {
            alert('등록 완료되었습니다.');
            location.replace(`/board/${articleId}`);
        }
        function fail() {
            alert('등록 실패했습니다.');
            location.replace(`/board/${articleId}`);
        }

        httpRequest('POST',`/api/articles/${articleId}/comments`, body, success, fail)
    });
}

const commentModifyButton = document.getElementById('comment-modify-btn');

if (commentModifyButton) {



    commentModifyButton.addEventListener('click', event => {
        body = JSON.stringify({
            content: document.getElementById('textarea-comment').value
        });

        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/board/${articleId}`);
        }
        function fail() {
            alert('수정 실패했습니다.');
            location.replace(`/board/${articleId}`);
        }

        const commentId = document.getElementById('comment-id').value;

        httpRequest('PUT',`/api/articles/${articleId}/comments/${commentId}`, body, success, fail)
    })
}