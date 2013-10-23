
Ext.ns('MapRed.wizards');

MapRed.wizards.SelectUPPMAXCard = Ext.extend(Ext.ux.Wiz.Card, {

	serverField : null,

	userField : null,

	passwordField : null,

	sftpModeBox : null,

    folder: "",

	initComponent : function() {

		this.serverField = new Ext.form.TextField({
			id : "sftp-server",
			name : 'server',
			value : 'sftp://kalkyl.uppmax.uu.se',
			fieldLabel : 'SFTP-Server',
			allowBlank : false,
			disabled :true
		});

		this.userField = new Ext.form.TextField({
			id : "username",
			name : 'username',
			fieldLabel : 'Username',
			value : '',
			allowBlank : false
		});

		this.passwordField = new Ext.form.TextField({
			id : "password",
			name : 'password',
			fieldLabel : 'Password',
			allowBlank : false,
			inputType : 'password',
			value : ''
		});
		
		this.portField = new Ext.form.TextField({
			id : "port",
			name : 'port',
			fieldLabel : 'ssh port',
			allowBlank : false,
			value : '22',
			hidden : true
		});


		Ext.apply(this, {
			id : 'card1',
			wizRef : this,
			title : 'Import from UPPMAX project',
			monitorValid : true,
			frame : false,
			fileUpload : true,
			border : false,
			height : '100%',
			folder: this.folder,
			defaults : {
				labelStyle : 'font-size:11px'
			},
			items : [
					{
						border : false,
						bodyStyle : 'background:none;padding-bottom:30px;',
						html : 'PleasePlease specify your UPPMAX username and password, optional type in your project ID otherwise all your projects will be listed.'
					},
					{
						title : '',
						id : 'fieldset-target',
						xtype : 'fieldset',
						autoHeight : true,
						defaults : {
							width : 210,
							labelStyle : 'font-size:11px'
						},
						defaultType : 'textfield',
						items : [ new Ext.form.TextField({
							id : 'path',
							fieldLabel : 'Import to Folder',
							allowBlank : false,
							value: this.folder
						}) ]
					},
{
						title : 'Define UPPMAX project',
						id : 'fieldset-uppmax-proj',
						xtype : 'fieldset',
						autoHeight : true,
						defaults : {
							width : 210,
							labelStyle : 'font-size:11px'
						},
						defaultType : 'textfield',
						items : [ new Ext.form.TextField({
							id : 'uppmax-project-id',
							fieldLabel : 'Project id',
							allowBlank : true
						}) ]
					},
					{
						title : 'SFTP-Server',
						id : 'fieldset-sftp',
						xtype : 'fieldset',
						autoHeight : true,
						defaults : {
							width : 210,
							labelStyle : 'font-size:11px'
						},
						defaultType : 'textfield',
						items : [ this.serverField,this.portField,
								this.userField, this.passwordField ]
					} ]
		});

		// call parent
		MapRed.wizards.SelectUPPMAXCard.superclass.initComponent.apply(this,
				arguments);

	},

});

