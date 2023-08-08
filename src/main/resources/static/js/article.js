const deleteButton = document.getElementById('delete-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        function success() {
            alert('삭제가 완료되었습니다.');
            location.replace('/board');
        }

        function fail() {
            alert('삭제 실패했습니다.');
            location.replace('/board');
        }

        httpRequest('DELETE',`/api/articles/${id}`, null, success, fail);
    });
}

const modifyButton = document.getElementById('modify-btn');

if (modifyButton) {
    modifyButton.addEventListener('click', event => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        let body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        })

        function success() {
            alert('수정 완료되었습니다.');
            location.replace(`/board/${id}`);
        }

        function fail() {
            alert('수정 실패했습니다.');
            location.replace(`/board/${id}`);
        }

        httpRequest('PUT',`/api/articles/${id}`, body, success, fail);
    });
}

const createButton = document.getElementById('create-btn');

if (createButton) {
    createButton.addEventListener('click', event => {

        let body = JSON.stringify({
            title: document.getElementById('title').value,
            content: document.getElementById('content').value
        });
        function success() {
            alert('등록 완료되었습니다.');
            location.replace('/board');
        }
        function fail() {
            alert('등록 실패했습니다.');
            location.replace('/board');
        }

        httpRequest('POST','/api/articles', body, success, fail)
    });
}


function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: body,
    }).then(response => {
        if (response.status === 200 || response.status === 201) {
            return success();
        }
        if (response.status === 401) {
            return fail();
        } else {
            return fail();
        }
    });
}