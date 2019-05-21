export default function ($) {

    function compareObjectDesc(a, b) {
        if (!a || !b){
            return 0
        }
        if (a.key !== b.key) {
            return ((a.value < b.value) ? 1 : ((a.value > b.value) ? -1 : 0));
        } else if (typeof a.child === 'undefined' && typeof b.child === 'undefined') {
            return ((a.value < b.value) ? 1 : ((a.value > b.value) ? -1 : 0));
        } else if (typeof a.child !== 'undefined' && typeof b.child !== 'undefined') {
            return compareObjectDesc(a.child, b.child);
        } else {
            return typeof a.child !== 'undefined' ? 1 : -1;
        }
    }

    function compareObjectAsc(a, b) {
        if (!a || !b){
            return 0
        }
        if (a.key !== b.key) {
            return ((a.value < b.value) ? -1 : ((a.value > b.value) ? 1 : 0));
        } else if (typeof a.child === 'undefined' && typeof b.child === 'undefined') {
            return ((a.value < b.value) ? -1 : ((a.value > b.value) ? 1 : 0));
        } else if (typeof a.child !== 'undefined' && typeof b.child !== 'undefined') {
            return compareObjectAsc(a.child, b.child);
        } else {
            return typeof a.child !== 'undefined' ? 1 : -1;
        }
    }

    $.fn.dataTableExt.oSort['custom-asc'] = function (a, b) {
        return compareObjectAsc(a, b);
    };

    $.fn.dataTableExt.oSort['custom-desc'] = function (a, b) {
        return compareObjectDesc(a, b);
    };

    if (!$.fn.dataTable) throw new Error('treeTable requires dataTables.js');

    const DataTable = $.fn.dataTable;

    const TreeTable = function (element, options) {
        const self = this;
        this.displayed = new Set([]);
        this.$el = $(element);
        this.dt = null;
        const initialOrder = options.order;
        options.order = [];
        options.columns = options.columns || [];
        options.columns.map((col) => {
            const oldRender = col.render;
            col.render = function (data, type, full, meta) {
                switch (type) {
                    case "sort":
                        return self.buildOrderObject(full['key'], col["data"]).child;
                    default:
                        return oldRender ? oldRender(data, type, full, meta) : data;
                }
            };
            col.type = "custom";
        });

        options.columns = [{
            "class": "details-control",
            "orderable": false,
            "data": null,
            "defaultContent": "<div class='expander'></div>",
            "width": 50,
            "createdCell": function (td, cellData, rowData) {
                if (rowData.level > 0) {
                    td.className = td.className + ' level-' + rowData.level;
                }
            }
        }, {
            "data": "level",
            "visible": false
        }, {
            "data": "key",
            "visible": false
        }, {
            "data": "parent",
            "visible": false
        }].concat(options.columns);

        this.dt = this.$el.on('init.dt', function () {
           self.$el.DataTable().columns([3]).search('^(0)$', true, false).draw();
        }).DataTable(options);

        this.$el.find('tbody').on('click', 'tr td:first-child', function () {
            self.toggleChildRows($(this))
        });

        this.dt.order(initialOrder);

    };

    TreeTable.prototype.toggleChildRows = function ($cell) {
        const tr = $cell.closest('tr');
        const row = this.dt.row(tr);
        const key = row.data().key;
        if (this.displayed.has(key)) {
            this.displayed.delete(key);
            tr.removeClass('open');
        } else {
            this.displayed.add(key);
            tr.addClass('open');
        }
        let regex = "^(0";
        this.displayed.forEach(function (value) {
            regex = regex + "|" + value;
        });
        regex = regex + ")$";
        this.dt.columns([3]).search(regex, true, false).draw();
    };


    TreeTable.DEFAULTS = {};

    TreeTable.prototype.buildOrderObject = function(key, column) {
        if (!this.dt) return '';

        const rowData = this.dt.row(key - 1).data();
        if (typeof rowData === 'undefined') {
            return {};
        } else {
            const parent = this.buildOrderObject(rowData['parent'], column);
            let a = parent;
            while (typeof a.child !== 'undefined') {
                a = a.child;
            }
            a.child = {};
            a.child.key = rowData['key'];
            a.child.value = rowData[column];
            return parent;
        }
    };

    const old = $.fn.treeTable;

    $.fn.treeTable = function (option) {
        return this.each(function () {
            const $this = $(this);
            let data = $this.data('treeTable');
            const options = $.extend({}, TreeTable.DEFAULTS, $this.data(), typeof option === 'object' && option);

            if (!data) $this.data('treeTable', (data = new TreeTable(this, options)));
        });
    };

    $.fn.treeTable.Constructor = TreeTable;

    $.fn.treeTable.noConflict = function () {
        $.fn.treeTable = old;
        return this;
    };

}