/* Base URL of the web-service for the current user */
var wsBase = 'http://localhost:8080/bmt/login1-login2-login3/'
/* Shows the identity of the current user */ 
function setIdentity() {
	//TODO 1
 
	var path = wsBase.split('/')
	var identity = path[path.length - 2]
	$('h1 .identity').text(identity)
 
}

/* Sets the height of <div id="#contents"> to benefit from all the remaining place on the page */
function setContentHeight() {
	// TODO 2
 
	var availableHeight = $(window).height()
	availableHeight -= $('#contents').offset().top
	availableHeight -= 2 * $('h1').offset().top
	availableHeight -= 4*1
	$('#contents').height(availableHeight)
 
}


/* Selects a new object type : either "bookmarks" or "tags" */
function selectObjectType(type) {
	// TODO 3
 
	var isSelected = $('#menu .' + type).hasClass('selected')
	if (!isSelected) {
		$('#menu li').removeClass('selected')
		$('#add > *').removeClass('selected')
		$('#menu li.' + type).addClass('selected')
		var isTags = type == "tags"
		if (isTags) {
			listTags()
			$('#add .tag').addClass('selected')
		} else {
			listBookmarks()
			$('#add .bookmark').addClass('selected') // Added for question 11 
		}
	}
 
}

 
/* Removes all contents from <div id="items"> */
function clearItems() {
	$('#items').children().remove()
}
 
/* Loads the list of all bookmarks and displays them */
function listBookmarks() {
	//TODO 4
 
	clearItems()
	var url = wsBase + "bookmarks"
	$.get(url, function(data) {
		var bms = eval(data)
		for (var i=0 ; i<bms.length ; i++) {
			var bm = bms[i]
			var $bm = $('.model.bookmark').clone()
			$bm.removeClass('model')
			$bm.addClass('item')
			$bm.find('h2').text(bm.title)
			$bm.attr('num', bm.id)
			$bm.find('a').attr('href', bm.link)
			$bm.find('a').text(bm.link)
			$bm.find('.description').text(bm.description ? bm.description : '')
			var $tags = $bm.find('.tags')
			for (var j=0 ; j<bm.tags.length ; j++) {
				$tags.append('<li num="'+bm.tags[j].id+'">'+bm.tags[j].name+'</li>') // id added for question 12
			}
			$('#items').append($bm)
		}
	}, 'json')
 
}

/* Loads the list of all tags and displays them */
function listTags() {
	//TODO 5
 
	clearItems()
	var url = wsBase + "tags"
	$.get(url, function(data) {
		var tags = eval(data)
		for (var i=0 ; i<tags.length ; i++) {
			var tag = tags[i]
			var $tag = $('.model.tag').clone()
			$tag.removeClass('model')
			$tag.addClass('item')
			$tag.find('h2').text(tag.name)
			$tag.attr('num', tag.id)
			$('#items').append($tag)
		}
	}, 'json')
 
}

/* Adds a new tag */
function addTag() {
	//TODO 6
 
	var url = wsBase + "tags"
	var name = $('input[name="name"]').attr('value')
	if (name == '') {
		alert('You must specify a tag name!')
	} else {
		var newTag = { "name" : name }
		$.post(url, "json=" + JSON.stringify(newTag,null,2)).always(listTags)
	}
 
}

/* Handles the click on a tag */
function clickTag() {
	//TODO 7
 

	var $tag = $(this)
	var $oldTag = $('#items .item.selected')

	if ($oldTag.size() == 1 && $oldTag.attr('num') == $tag.attr('num')) {
		// already selected
		return
	}

	if ($oldTag.size() == 1 && $oldTag.attr('num') != $tag.attr('num')) {
		// deselect previously selected tag
		$oldTag.removeClass('selected')
		var $h2 = $oldTag.find('h2')
		$h2.show()
		$h2.nextAll().remove()
	}

	// select new tag
	$tag.addClass('selected')
	var $h2 = $tag.find('h2')
	$h2.hide()
	$h2.after('<input type="text" name="name_mod" value="' + $h2.text() + '"><input type="button" name="modify" value="Modify name"> <input type="button" name="remove" value="Remove tag">')
	$('input[type="button"][name="modify"]', $tag).on('click', modifyTag)
	$('input[type="button"][name="remove"]', $tag).on('click', removeTag)
 
}

/* Performs the modification of a tag */
function modifyTag() {
	//TODO 8
 
	var $tag = $(this).parents('.tag')
	var url = wsBase + "tags/" + $tag.attr('num')
	var name = $tag.find('input[name="name_mod"]').attr('value')
	var tag = { "id" : $tag.attr('num'), "name": name }
	$.post(url, "json=" + JSON.stringify(tag) + "&x-http-method=put").always(listTags)
 
}

/* Removes a tag */
function removeTag() {
	//TODO 9
 
	var $tag = $(this).parents('.tag')
	var url = wsBase + "tags/" + $tag.attr('num')
	$.post(url, "x-http-method=delete").always(listTags)
 
} 
 
/* Question 11 - Creates a form for new bookmark specification */
function newBookmark() {

	// Create a form for specifying the new bookmark name, url and description
	var $div = $('#add .bookmark div')
	$div.empty()
	createBookmarkTable($div, null)
	
	// clickable Create button
	var $tbody = $div.find('tbody')
	$tbody.before('<tfoot><tr><td colspan="2"><input type="button" value="Create !" name="create"></td></tr>')
	$div.find('input[name="create"]').click(createBookmark)
}

/**
 * Creates a table for creating/modifying a bookmark.
 *
 * @param $parent parent element
 * @param bm a bookmark
 */
function createBookmarkTable($parent, bm) {
	$parent.append('<table><col><col><tbody></tbody></table>')
	var $tbody = $parent.find('tbody')
	$tbody.append('<tr><th>Name</th><td><input type="text" name="bm.name" value="'+(bm && bm.title ? bm.title : '')+'"></td>')
	$tbody.append('<tr><th>URL</th><td><input type="text" name="bm.link" value="'+(bm && bm.link ? bm.link : '')+'"></td>')
	$tbody.append('<tr><th>Description</th><td><textarea name="bm.description">'+(bm && bm.description ? bm.description : '')+'</textarea></td>')
	$tbody.append('<tr><th>Tags</th><td><ul></ul></td>')
	
	// add all current tags
	$ul = $tbody.find('ul')
	var url = wsBase + "tags"
	$.get(url, function(data) {
		var tags = eval(data)
		for (var i=0 ; i<tags.length ; i++) {
			var tag = tags[i]
			var li = '<li><input type="checkbox" name="tag.'+tag.id+'" value="'+tag.name+'" '
			var takeTag = false
			if (bm && bm.tags) {
				for (var j = 0; j<bm.tags.length; j++) {
					if (bm.tags[j].id == tag.id) {
						takeTag = true
						break
					}
				}
			}
			if (takeTag)
				li += 'checked="checked"'
			li += '> '+tag.name+'</li>'
			$ul.append(li)
		}
	}, 'json')
	
	return $tbody.parent()
}

function getBookmarkFromTable($table) {
	var name = $table.find('input[name="bm.name"]').val()
	var description = $table.find('textarea[name="bm.description"]').val()
	var link = $table.find('input[name="bm.link"]').val()
	var bm = { title : name, description: description, link: link, tags: []}
	
	// Append all selected tags to 'bm'
	$table.find('ul li input:checked').each(function (i,e) {
		var tagId = $(e).attr('name').substring(4)
		var tagName = $(e).attr('value')
		bm.tags[bm.tags.length] = { id : tagId, name : tagName }
	})
	
	return bm
}

/* Question 11 - Creates a new bookmark */
function createBookmark() {
	// Create the new bookmark 'bm'
	var $div = $('#add .bookmark div')
	var $table = $div.find('table')
	var bm = getBookmarkFromTable($table)
	
	// Send to server
	var url = wsBase + "bookmarks"
	$.post(url, "json=" + JSON.stringify(bm,null,2)).always(function() { $div.empty() ; listBookmarks() })
}

/* Questions 10 & 12 - bookmark deletion and modification */
function clickBookmark() {
	var $bm = $(this)
	var $oldBm = $('#items .item.selected')

	if ($oldBm.size() == 1 && $oldBm.attr('num') == $bm.attr('num')) {
		// already selected
		return
	}

	if ($oldBm.size() == 1 && $oldBm.attr('num') != $bm.attr('num')) {
		// deselect previously selected bookmark
		$oldBm.removeClass('selected')
		$oldBm.find('h2').show()
		$oldBm.find('a').show()
		$oldBm.find('.description').show()
		$oldBm.find('.tags').show()
		$oldBm.find('.tags').nextAll().remove()
	}

	// select new bookmark
	$bm.addClass('selected')
	var $h2 = $bm.find('h2')
	var $a = $bm.find('a')
	var $description = $bm.find('.description')
	var $tags = $bm.find('.tags')
	$h2.hide()
	$a.hide()
	$description.hide()
	$tags.hide()
	
	var bm = getBookmarkFromItem($bm)
	var $table = createBookmarkTable($tags.parent(), bm)
	$table.after('<input type="button" value="Modify" onclick="modifyBookmark()"> <input type="button" value="Remove" onclick="removeBookmark()">')
}

/* Provides a bookmark from an item */
function getBookmarkFromItem($item) {
	var id = $item.attr('num')
	var name = $item.find('h2').text()
	var description = $item.find('.description').text()
	var link = $item.find('a').attr('href')
	var bm = { id : id, title : name, description: description, link: link, tags: []}
	$item.find('ul.tags li').each(function (i,e) {
		var tid = $(e).attr('num')
		var tname = $(e).text()
		bm.tags[bm.tags.length] = { id : tid, name: tname }
	})
	
	return bm
}

/* Bookmark modification */
function modifyBookmark() {
	var $bm = $('.bookmark.selected')
	var $table = $bm.find('table')
	var bm = getBookmarkFromTable($table)
	bm.id = $bm.attr('num')
	var url = wsBase + "bookmarks/" + $bm.attr('num')
	$.post(url, "json=" + JSON.stringify(bm) + "&x-http-method=put").always(listBookmarks)
}

/* Bookmark deletion */
function removeBookmark() {
	var $bm = $('.bookmark.selected')
	var url = wsBase + "bookmarks/" + $bm.attr('num')
	$.post(url, "x-http-method=delete").always(listBookmarks)
}
 
/* On document loading */
$(function() {
	// Put the name of the current user into <h1>
	setIdentity()

	// Adapt the height of <div id="contents"> to the navigator window
	setContentHeight()
 
	// Question 11 - Create the 'Add tag' button
	$('#add').append('<div class="bookmark"><input type="button" name="addBookmark" value="New bookmark" onclick="newBookmark()"><div></div></div>')
 
	// Listen to the clicks on menu items
	$('#menu li').on('click', function() {
		var isTags = $(this).hasClass('tags')
		selectObjectType(isTags ? "tags" : "bookmarks")
	})

	// Initialize the object type to "bookmarks"
	selectObjectType("bookmarks")

	// Listen to clicks on the "add tag" button
	$('#addTag').on('click', addTag)

	// Listen to clicks on the tag items
	$(document).on('click','#items .item.tag',clickTag)
 	
	// Questions 10 & 12 - Listen to clicks on bookmark items
	$(document).on('click','#items .item.bookmark',clickBookmark)

 
})
